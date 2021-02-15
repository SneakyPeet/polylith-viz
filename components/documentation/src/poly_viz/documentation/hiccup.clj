(ns poly-viz.documentation.hiccup
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.pprint :as pprint]))


(defn- definition-type [t]
  (let [c
        (get {"data" "is-warning"
              "function" "is-info"
              "macro" "is-danger"} t "")]
    [:span.tag.is-normal.is-light.mb-0 {:class c} t]))


(defn documentation-component [{:keys [interfaces] :as ws}]
  [:div.columns
   [:div.column.is-narrow
    [:aside.menu
     [:p.menu-label "Interfaces"]
     [:ul.menu-list
      (->> interfaces
           (map
            (fn [{:keys [name]}]
              [:li [:a {:href (str "#" name)} name]])))]]]
   [:div.column {:style "max-height: 90vh"}
    (->> interfaces
         (map
          (fn [{:keys [name definitions implementing-components]}]
            [:div
             [:h1.title {:id name} name]
             [:hr]
             [:div.mb-6
              (->> definitions
                   (map (fn [{:keys [type name] :as d}]
                          (let [data (dissoc d :name :type)]
                            [:div.block
                             [:div.tags.has-addons.mb-0
                              [:span.tag.has-text-weight-bold.mb-0 name " "]
                              (definition-type type)]
                             (when-not (empty? data)
                               [:pre.p-1.pl-4 (with-out-str
                                                (pprint/pprint data))])]))))]])))]])
