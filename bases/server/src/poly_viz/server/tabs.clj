(ns poly-viz.server.tabs
  (:require [poly-viz.workspace.interface :as f]
            [poly-viz.vis-network.interface :as vis]
            [poly-viz.documentation.interface :as docs]
            [poly-viz.search.interface :as search]
            [hiccup.page :as hp]))


(def ^:private tab-js "
function page(name) {
  document.getElementsByClassName('is-active')[0].className = 'navbar-item';
  document.getElementById('tab-' + name).className = 'navbar-item is-active has-text-info';
  var content = document.getElementsByClassName('tab-content');
  for(let i = 0; i < content.length; i++) {
    content[i].className = 'tab-content is-hidden';
  }
  document.getElementById(name).className = 'tab-content';
}
")

(defn ->html [opts ws tabs]
   (hp/html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:link {:rel "stylesheet"
             :href "https://cdn.jsdelivr.net/npm/bulma@0.9.1/css/bulma.min.css"}]]
    [:body
     [:div.container
      [:nav.navbar {:role "navigation" :aria-label "main navigation"}
       [:div.navbar-brand
        [:span.navbar-item.heading {:style "padding-top: 0.8rem"}
         [:strong (:name ws)]
         (when-let [v (:version opts)]
           [:span "|" v])]]
       [:div.navbar-menu
        [:div.navbar-start
         (->> tabs
              (map-indexed
                (fn [i {:keys [name]}]
                  (let [id (str "tab-" name)]
                    [:a.navbar-item
                     {:class   (when (zero? i) "is-active has-text-info")
                      :id      id
                      :onclick (str "page('" name "')" )}
                     name]))))]]]]
     [:div.block]
     [:div.container
      (->> tabs
           (map-indexed
             (fn [i {:keys [name component]}]
               [:div.tab-content
                {:id    name
                 :class (when-not (zero? i) "is-hidden")}
                component])))]
     [:script tab-js]]))


(defn- network [{:keys [include-dev-projects?
                        brick-options
                        brick-levels
                        vis-options]} ws]
  (let [network (vis/ws->network ws
                                 :include-dev-projects? include-dev-projects?
                                 :brick-options brick-options
                                 :brick-levels brick-levels)
        component (vis/network-vis-component network :vis-options vis-options)]
    component))


(defn tabs [{:keys [ws-path extra-pages] :as opts}]
  (let [ws (f/enrich (f/from-path ws-path))]
    (->html
      opts
      ws
      (into [{:name      "Deps"
              :component (network opts ws)}
             {:name      "Explore"
              :component (docs/documentation-component ws)}
             {:name      "Search"
              :component (search/search-component ws)}
             ]
            extra-pages))))
