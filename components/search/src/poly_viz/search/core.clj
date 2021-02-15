(ns poly-viz.search.core)


(defn ws->search [ws]
  (->> ws
       :interfaces
       (map (fn [{:keys [name] :as i}]
              (update i :definitions
                      (fn [d] (map #(assoc % :interface name) d)))))))
