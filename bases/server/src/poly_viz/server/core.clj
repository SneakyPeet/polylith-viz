(ns poly-viz.server.core
  (:require [aleph.http :as http]
            [clojure.java.browse :as browser]
            [clojure.string :as string]
            [poly-viz.server.tabs :as tabs]
            [poly-viz.vis-network.interface :as vis]
            [poly-viz.workspace.interface :as workspace]
            [clojure.java.io :as io]
            [poly-viz.server.static]))


(defn- open-file [req]
  (let [file (second (string/split (:query-string req) #"="))]
    (if (java.awt.Desktop/isDesktopSupported)
      (do
        (.open (java.awt.Desktop/getDesktop) (io/file file))
        {:status 200
         :body (str "opening: " file)})
      {:status 200
       :body "not supported"})))


(defn- handler [opts]
  (fn [req]
    (let [uri (:uri req)]
      (case uri
        "/open" (open-file req)
        (try
          (when (:output-ws-on-request? opts)
            (apply workspace/poly-shell! (:ws-shell-cmd opts)))
          {:status 200
           :headers {"content-type" "text/html"}
           :body (tabs/tabs opts)}
          (catch Exception e
            {:status 500
             :headers {"content-type" "text/plain"}
             :body (str e)}))))))


(defonce ^:private *server (atom nil))


(defn stop []
  (when-let [s @*server]
    (.close s)
    (reset! *server nil)))


(defn start
  "Starts a server hosting the polylith-viz info pages"
  [& {:keys [port ws-path browse?
             include-dev-projects?
             output-ws-on-request? ws-shell-cmd
             brick-options
             brick-levels
             vis-options]
      :or {port 8087
           ws-path "polyws.edn"
           output-ws-on-request? true
           ws-shell-cmd ["poly" "ws" "out:polyws.edn"]
           browse? true
           include-dev-projects? false
           brick-options vis/default-brick-vis-options
           brick-levels vis/brick-hierarchical-layout-starting-levels
           vis-options vis/default-vis-options}}]
  (let [opts {:ws-path ws-path
              :output-ws-on-request? output-ws-on-request?
              :ws-shell-cmd ws-shell-cmd
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
