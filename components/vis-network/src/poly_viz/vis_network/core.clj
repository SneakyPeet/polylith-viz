(ns poly-viz.vis-network.core)


(defn- ->node [brick-options brick-levels edge-levels {:keys [name type]}]
  (let [type (keyword type)
        opts (get-in brick-options [type :nodes] {})]
    (assoc opts
           :label name
           :id name
           :level (+ (get brick-levels type 0) (get edge-levels name 0)))))


(defn- ws->nodes
  [brick-options brick-levels edge-levels {:keys [projects bases components]}]
  (->>
   [components
    bases
    projects]
   (reduce into)
   (map (partial ->node brick-options brick-levels edge-levels))))


(defn- brick->edges [opts deps-key brick]
  (let [from (:name brick)
        tos (get brick deps-key [])]
    (map #(assoc opts :from from :to (str %)) tos)))


(defn- ws->edges
  [brick-options {:keys [projects bases components]}]
  (let [->edges (fn [t k]
                  (fn [brick]
                    (brick->edges (get-in brick-options [t :edges] {}) k brick)))]
    (->>
     [(map (->edges :component :interface-deps) components)
      (map (->edges :base :interface-deps) bases)
      (map (->edges :project :base-names) projects)]
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
