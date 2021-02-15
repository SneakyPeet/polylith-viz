(ns poly-viz.server.core
  (:require [poly-viz.workspace.interface :as f]
            [poly-viz.vis-network.interface :as vis]
            [poly-viz.documentation.interface :as docs]
            [aleph.http :as http]
            [hiccup.page :as hp]
            [clojure.java.browse :as browser]))


(defn ->html [component]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (hp/html5
          [:head]
          [:body
           component])})


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
      "#network {height: 100vh}"]
     component]))


(defn- documents [ws]
  (docs/documentation-component (docs/ws->docs ws)))


(defn- handler [{:keys [ws-path] :as opts}]
  (fn [req]
    (try
      (let [ws (f/from-path ws-path)]
        (->html
         [:div
          (documents ws)
          (network opts ws)]))
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
