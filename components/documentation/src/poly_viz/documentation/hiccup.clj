(ns poly-viz.documentation.hiccup
  (:require [cheshire.core :as json]
            [clojure.string :as string]))

(def ^:private column-style  "max-height: 90vh; overflow-y: auto;")

(defn documentation-component [{:keys [interfaces] :as enriched-ws}]
  [:div.columns
   [:div.column.is-one-quarter {:style column-style}
    [:aside.menu
     [:p.menu-label "Interfaces"]
     [:ul.menu-list
      (->> interfaces
           (map
            (fn [{:keys [name]}]
              [:li [:a {:href (str "#" name)} name]])))]]]
   [:div.column.is-three-quarters {:style column-style}
    (->> interfaces
         (map
          (fn [{:keys [name definitions implementing-components]}]
            [:div
             [:h1.title {:id name} name]
             [:hr]
             [:div.mb-6
              (map :display-component definitions)]])))]])
