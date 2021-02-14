(ns poly-viz.vis-network.hiccup
  (:require [cheshire.core :as json]))

(def ^:private vis-js "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js")

(defn ws-network-vis-component [network vis-opts]
  (let [data-json (json/generate-string network {:escape-non-ascii true})
        options-json (json/generate-string vis-opts {:escape-non-ascii true})]
    [:div
     [:div#network]
     [:script {:type "text/javascript" :src vis-js}]
     [:script {:type "text/javascript"}
      "var VIS_DATA = " data-json  ";"
      "var container = document.getElementById('network');"
      "var nodes = new vis.DataSet(VIS_DATA.nodes);"
      "var edges = new vis.DataSet(VIS_DATA.edges);"
      "var data = {nodes: nodes, edges: edges};"
      "var options = " options-json ";"
      "var network = new vis.Network(container, data, options);"]]))
