(ns poly-viz.vis-network.core)


(defn- ->node [style levels {:keys [name type]}]
  (let [type (keyword type)]
    {:name name
     :id name
     :color (get-in style [type :color])
     :level (get levels type)
     }))


(defn- ws->nodes
  [style levels {:keys [projects bases components]}]
  (->>
   [components
    bases
    projects]
   (reduce into)
   (map (partial ->node style levels))))


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
  {:project {:color "blue"}
   :component {:color "yellow"}
   :base {:color "green"}})


(def level-defaults
  {:project 1
   :base 2
   :component 3})


(defn ws->network
  [ws {:keys [include-dev?
              style
              levels]
       :or {include-dev? false
            style style-defaults
            levels level-defaults}}]
  (let [ws (update ws :projects (fn [projects]
                                  (cond->> projects
                                    (false? include-dev?) (filter #(false? (:is-dev %))))))]
    {:nodes (ws->nodes style levels ws)
     :edges (ws->edges ws)}))


(comment

  (ws->network (poly-viz.workspace.interface/from-path "ws.edn") {})

  )
