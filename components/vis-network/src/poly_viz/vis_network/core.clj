(ns poly-viz.vis-network.core)


(defn- brick->id [brick]
  (str (:name brick) "-" (name (:type brick))))


(defn- ->node [brick-options brick-levels edge-levels {:keys [name type] :as brick}]
  (let [type (keyword type)
        opts (get-in brick-options [type :nodes] {})]
    (assoc opts
           :label name
           :id (brick->id brick)
           :level (+ (get brick-levels type 0) (get edge-levels name 0)))))


(defn- ws->nodes
  [brick-options brick-levels edge-levels {:keys [projects bases components]}]
  (->>
   [components
    bases
    projects]
   (reduce into)
   (map (partial ->node brick-options brick-levels edge-levels))))


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

        edges (ws->edges brick-options ws)
        edge-levels (->> edges
                         (map :to)
                         (frequencies))]
    {:nodes (ws->nodes brick-options brick-levels edge-levels ws)
     :edges edges}
    ))
