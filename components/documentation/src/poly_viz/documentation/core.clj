(ns poly-viz.documentation.core)


(defn ws->documentation [ws]
  (->> ws
       :interfaces
       (map (fn [{:keys [name] :as i}]
              (update i :definitions
                      (fn [d] (map #(assoc % :interface name) d))))))) 


(->> (poly-viz.workspace.interface/from-path "ws.edn")
     :interfaces
     (map (fn [{:keys [name] :as i}]
            (update i :definitions
                    (fn [d] (map #(assoc % :interface name) d))))))



(defn documentation-component [docs])
