(ns poly-viz.search.interface
  (:require [poly-viz.search.hiccup :as h]))


(defn search-component [enriched-ws]
  (h/search-component enriched-ws))
