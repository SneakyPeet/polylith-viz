(ns poly-viz.vis-network.interface
  (:require [poly-viz.vis-network.core :as c]
            [poly-viz.vis-network.options :as o]
            [poly-viz.vis-network.hiccup :as h]))


(def ^{:doc "option modules can be found here https://visjs.github.io/vis-network/docs/network/"}
  default-vis-options o/default-vis-options)


(def ^{:doc "vis options for brick nodes https://visjs.github.io/vis-network/docs/network/nodes.html
and edges https://visjs.github.io/vis-network/docs/network/edges.html"}
  default-brick-vis-options o/default-brick-vis-options)


(def ^{:doc "When using the hierarchical layout,
a brick level is determined by this starting-level + number of other bricks depending on it."}
  brick-hierarchical-layout-starting-levels o/brick-hierarchical-layout-starting-levels)


(defn ws->network
  "Generates https://visjs.github.io/vis-network/docs/network/ nodes and edges"
  [ws & {:keys [include-dev-projects?
                brick-options
                brick-levels]
         :or {include-dev-projects? false
              brick-options default-brick-vis-options
              brick-levels brick-hierarchical-layout-starting-levels}}]
  (c/ws->network
   ws
   {:include-dev? include-dev-projects?
    :brick-options brick-options
    :brick-levels brick-levels}))


(defn network-vis-component
  "Generates a hiccup component for the workspace vis network"
  [network & {:keys [vis-options]
                                       :or {vis-options default-vis-options}}]
  (h/ws-network-vis-component network vis-options))


(comment

  (ws->network (poly-viz.workspace.interface/from-path "ws.edn"))

  (network-vis-component
   (ws->network (poly-viz.workspace.interface/from-path "ws.edn")))

  )
