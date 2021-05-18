(ns poly-viz.vis-network.core)


(defn- brick->id [brick]
  (str (:name brick) "-" (name (:type brick))))


(defn- calculate-depth [depths deps-lookup component]
  (let [deps (get deps-lookup component [])]
    (if (empty? deps)
      depths
      (reduce
        (fn [d i] (calculate-depth (update d i inc) deps-lookup i))
        depths
        deps))))


(defn- components-depth [components]
  (let [initial-depth (->> components
                           (map (juxt :name (constantly 0)))
                           (into {}))

        deps-lookup (->> components
                         (map (juxt :name :interface-deps))
                         (into {}))]
    (reduce (fn [d i]
              (calculate-depth d deps-lookup i))
            initial-depth
            (keys deps-lookup))))


(defn- ->node [brick-options brick-levels component-depth-lookup {:keys [name type] :as brick}]
  (let [type (keyword type)
        opts (get-in brick-options [type :nodes] {})
        id (brick->id brick)
        level (if (= :component type)
                (get component-depth-lookup name 0)
                0)]
    (assoc opts
           :label name
           :id id
           :level (+ (get brick-levels type 0) level))))


(defn- ws->nodes
  [brick-options brick-levels {:keys [projects bases components]}]
  (let [component-depth-lookup (components-depth components)]
    (->>
      [components
       bases
       projects]
      (reduce into)
      (map (partial ->node brick-options brick-levels component-depth-lookup)))))


(defn- brick->edges [opts deps-key brick-type brick]
  (let [from (brick->id brick)
        tos (get brick deps-key [])]
    (map #(assoc opts :from from :to (str % "-" (name brick-type))) tos)))


(defn- ws->edges
  [brick-options {:keys [projects bases components]}]
  (let [->edges (fn [t k brick-type]
                  (fn [brick]
                    (brick->edges (get-in brick-options [t :edges] {}) k brick-type brick)))]
    (->>
     [(map (->edges :component :interface-deps :component) components)
      (map (->edges :base :interface-deps :component) bases)
      (map (->edges :project :base-names :base) projects)]
     (reduce into)
     (reduce into))))


(defn ws->network
  [ws {:keys [include-dev? brick-options brick-levels]}]
  (let [ws (update ws :projects (fn [projects]
                                  (cond->> projects
                                    (false? include-dev?) (filter #(false? (:is-dev %))))))

        edges (ws->edges brick-options ws)]
    {:nodes (ws->nodes brick-options brick-levels ws)
     :edges edges}
    ))
