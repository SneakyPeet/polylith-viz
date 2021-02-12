(ns poly-viz.vis-network.core)


(defn- ->node [style levels edge-levels {:keys [name type]}]
  (let [type (keyword type)]
    {:label name
     :id name
     :color (get-in style [type :color])
     :level (+ (get levels type 0) (get edge-levels name 0))
     }))


(defn- ws->nodes
  [style levels edge-levels {:keys [projects bases components]}]
  (->>
   [components
    bases
    projects]
   (reduce into)
   (map (partial ->node style levels edge-levels))))


(defn- brick->edge [deps-key brick]
  (let [from (:name brick)
        tos (get brick deps-key [])]
    (map #(hash-map :from from :to (str %)) tos)))


(defn- ws->edges
  [{:keys [projects bases components]}]
  (->>
   [(map (partial brick->edge :interface-deps) components)
    (map (partial brick->edge :interface-deps) bases)
    (map (partial brick->edge :base-names) projects)]
   (reduce into)
   (reduce into)))


(def style-defaults
  {:project {:color "#ED553B"}
   :base {:color "#F6D55C"}
   :component {:color "#3CAEA3"}})


(def level-defaults
  {:project 0
   :base 0
   :component 2})


(defn ws->network
  [ws {:keys [include-dev?
              style
              levels]
       :or {include-dev? false
            style style-defaults
            levels level-defaults}}]
  (let [ws (update ws :projects (fn [projects]
                                  (cond->> projects
                                    (false? include-dev?) (filter #(false? (:is-dev %))))))

        edges (ws->edges ws)
        edge-levels (->> edges
                         (map :to)
                         (frequencies))]
    {:nodes (ws->nodes style levels edge-levels ws)
     :edges edges}
    ))


(comment

  (ws->network (poly-viz.workspace.interface/from-path "ws.edn") {})

  )
