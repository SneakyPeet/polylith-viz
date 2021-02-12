(ns poly-viz.html.vis-network
  (:require [poly-viz.vis-network.interface :as vis]
            [cheshire.core :as json]))

(def ^:private vis-js "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js")

(def ^:private options
  {
   :layout {:hierarchical {:enabled true
                           :levelSeparation 50
                           :nodeSpacing 250}}
   :edges {:arrows {:to {:enabled true
                         :type "arrow"
                         :scaleFactor 0.8}}
           :smooth true}

   :nodes {:shape "box"
           :margin {:top 10 :bottom 10 :left 20 :right 20}}
   })


(defn ws->hiccup [ws]
  (let [data-json (json/generate-string (vis/ws->network ws) {:escape-non-ascii true})
        options-json (json/generate-string options {:escape-non-ascii true})]
    [:div
     [:div#network
      {:style
        "height: 100vh;
         width: 100vw; "}]
     [:script {:type "text/javascript" :src vis-js}]
     [:script {:type "text/javascript"}
      "var VIS_DATA = " data-json  ";"
      "var container = document.getElementById('network');"
      "var nodes = new vis.DataSet(VIS_DATA.nodes);"
      "var edges = new vis.DataSet(VIS_DATA.edges);"
      "var data = {nodes: nodes, edges: edges};"
      "var options = " options-json ";"
      "var network = new vis.Network(container, data, options);"
      ]]))
