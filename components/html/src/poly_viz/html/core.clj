(ns poly-viz.html.core
  (:require [hiccup.page :as hp]
            [poly-viz.html.vis-network :as vis]))


(defn page [ws]
  (hp/html5
   [:head]
   [:body
    (vis/ws->hiccup ws)]))


(comment
  (->> (poly-viz.workspace.interface/from-path "ws.edn")
       page
       (spit "test.html"))
  
  )
