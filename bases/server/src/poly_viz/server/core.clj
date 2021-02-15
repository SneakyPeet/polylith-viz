(ns poly-viz.server.core
  (:require [poly-viz.workspace.interface :as f]
            [poly-viz.vis-network.interface :as vis]
            [poly-viz.documentation.interface :as docs]
            [poly-viz.search.interface :as search]
            [aleph.http :as http]
            [hiccup.page :as hp]
            [clojure.java.browse :as browser]))


(def ^:private tab-js "
function page(name) {
  document.getElementsByClassName('is-active')[0].className = '';
  document.getElementById('tab-' + name).className = 'is-active';
  var content = document.getElementsByClassName('tab-content');
  for(let i = 0; i < content.length; i++) {
    content[i].className = 'tab-content is-hidden';
  }
  document.getElementById(name).className = 'tab-content';
}
")

(defn ->html [tabs]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (hp/html5
          [:head
           [:meta {:charset "utf-8"}]
           [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
           [:link {:rel "stylesheet"
                   :href "https://cdn.jsdelivr.net/npm/bulma@0.9.1/css/bulma.min.css"}]]
          [:body
           [:section.section
            [:div.container
             [:div.tabs
              [:ul
               (->> tabs
                    (map-indexed
                     (fn [i {:keys [name]}]
                       (let [id (str "tab-" name)]
                         [:li {:class (when (zero? i) "is-active")
                               :id id}
                          [:a
                           {:onclick (str "page('" name "')" )}
                           name]]))))]]
             (->> tabs
                  (map-indexed
                   (fn [i {:keys [name component]}]
                     [:div.tab-content
                      {:id name
                       :class (when-not (zero? i) "is-hidden")}
                      component])))]]
           [:script tab-js]])})


(defn- network [{:keys [include-dev-projects?
                        brick-options
                        brick-levels
                        vis-options]} ws]
  (let [network (vis/ws->network ws
                                 :include-dev-projects? include-dev-projects?
                                 :brick-options brick-options
                                 :brick-levels brick-levels)
        component (vis/network-vis-component network :vis-options vis-options)]
    [:div
     [:style
      "#network {height: 90vh}"]
     component]))



(defn- handler [{:keys [ws-path] :as opts}]
  (fn [req]
    (try
      (let [ws (f/from-path ws-path)]
        (->html
         [{:name "Deps"
           :component (network opts ws)}
          {:name "Explore"
           :component (docs/documentation-component ws)}
          {:name "Search"
           :component (search/search-component ws)}]))
      (catch Exception e
        {:status 500
         :headers {"content-type" "text/plain"}
         :body (str e)}))))


(defonce ^:private *server (atom nil))


(defn stop []
  (when-let [s @*server]
    (.close s)
    (reset! *server nil)))


(defn start
  "Starts a server hosting the polylith-viz info pages"
  [& {:keys [port ws-path browse?
             include-dev-projects?
             brick-options
             brick-levels
             vis-options]
      :or {port 8087
           ws-path "ws.edn"
           browse? true
           include-dev-projects? false
           brick-options vis/default-brick-vis-options
           brick-levels vis/brick-hierarchical-layout-starting-levels
           vis-options vis/default-vis-options}}]
  (let [opts {:ws-path ws-path
              :include-dev-projects? include-dev-projects?
              :brick-options brick-options
              :brick-levels brick-levels
              :vis-options vis-options}
        app (handler opts)]
    (stop)
    (reset! *server (http/start-server app {:port port}))
    (when browse?
      (browser/browse-url (str "http://localhost:" port)))
    (str "Listening on Port " port)))


(comment
  (start)
  (stop)
  )
