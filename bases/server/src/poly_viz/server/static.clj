(ns poly-viz.server.static
  (:require [poly-viz.server.tabs :as tabs]
            [poly-viz.vis-network.interface :as vis]
            [poly-viz.workspace.interface :as workspace]))


(defn html
  "Returns the info pages as an html string"
  [& {:keys [ws-path
             include-dev-projects?
             ws-shell-cmd
             rebuild-ws-file-before-generate?
             brick-options
             brick-levels
             vis-options
             version
             extra-pages]
      :or {ws-path "polyws.edn"
           ws-shell-cmd ["poly" "ws" "out:polyws.edn"]
           rebuild-ws-file-before-generate? true
           include-dev-projects? false
           brick-options vis/default-brick-vis-options
           brick-levels vis/brick-hierarchical-layout-starting-levels
           vis-options vis/default-vis-options
           version ""
           extra-pages []}}]
  (let [opts {:ws-path ws-path
              :ws-shell-cmd ws-shell-cmd
              :include-dev-projects? include-dev-projects?
              :brick-options brick-options
              :brick-levels brick-levels
              :vis-options vis-options
              :version version
              :extra-pages extra-pages}]
    (when rebuild-ws-file-before-generate?
      (apply workspace/poly-shell! ws-shell-cmd))
    (tabs/tabs opts)))
