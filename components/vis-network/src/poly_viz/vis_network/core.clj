(ns poly-viz.vis-network.core)


(defn- brick->id [brick]
  (str (:name brick) "-" (name (:type brick))))


(defn- get-reversed-levels [components]
  (let [component-deps (->> components
                            (map (juxt :name :interface-deps))
                            (sort-by #(-> % last count))
                            vec)]
    (loop [component-deps component-deps
           result         {}]
      (if (empty? component-deps)
        result
        (let [head             (first component-deps)
              [component deps] head
              tail             (vec (rest component-deps))
              deps-levels      (map #(get result %) deps)
              can-calculate?   (empty? (filter nil? deps-levels))]
          (if-not can-calculate?
            (recur
              (conj tail head)
              result)
            (let [level (if (empty? deps-levels)
                          0
                          (inc (apply max deps-levels)))]
              (recur
                tail
                (assoc result component level)))))))))


(defn- components-depth [components]
  (let [reversed-levels (get-reversed-levels components)
        depth           (if (empty? reversed-levels)
                0
                (apply max (vals reversed-levels)))]
    (->> reversed-levels
         (map (fn [[k v]]
                [k (- depth v)]))
         (into {}))))


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
