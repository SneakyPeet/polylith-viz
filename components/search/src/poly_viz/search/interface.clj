(ns poly-viz.search.interface
  (:require [poly-viz.search.core :as c]
            [poly-viz.search.hiccup :as h]))


(defn ws->search [ws] (c/ws->search ws))


(defn search-component [search]
  (h/search-component search))
