(ns poly-viz.search.interface
  (:require [poly-viz.search.core :as c]
            [poly-viz.search.hiccup :as h]))


(defn search-component [ws]
  (h/search-component (c/ws->search ws)))
