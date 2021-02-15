(ns poly-viz.documentation.interface
  (:require [poly-viz.documentation.core :as c]
            [poly-viz.documentation.hiccup :as h]))


(defn ws->docs [ws] (c/ws->documentation ws))


(defn documentation-component [docs]
  (h/documentation-component docs))
