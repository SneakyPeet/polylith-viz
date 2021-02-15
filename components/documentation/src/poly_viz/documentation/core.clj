(ns poly-viz.documentation.core)


(defn ws->documentation [ws]
  (->> ws
       :interfaces
       (map (fn [{:keys [name] :as i}]
              (update i :definitions
                      (fn [d] (map #(assoc % :interface name) d))))))) 


(comment 
  (ws->documentation (poly-viz.workspace.interface/from-path "ws.edn"))
  )
