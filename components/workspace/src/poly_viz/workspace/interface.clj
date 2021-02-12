(ns poly-viz.workspace.interface
  (:require [poly-viz.workspace.core :as c]))


(defn from-path
  "Returns the polylith workspace hashmap read from the provided path"
  [path]
  (c/from-path path))
