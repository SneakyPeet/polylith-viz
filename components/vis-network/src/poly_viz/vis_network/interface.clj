(ns poly-viz.vis-network.interface
  (:require [poly-viz.vis-network.core :as c]))


(defn ws->network
  "Generates https://visjs.github.io/vis-network/docs/network/ nodes and edges"
  [ws & {:keys [include-dev?
                style
                levels]
         :or {include-dev? false
              style c/style-defaults
              levels c/level-defaults}}]
  (c/ws->network
   ws
   {:include-dev? include-dev?
    :style style
    :levels levels}))
