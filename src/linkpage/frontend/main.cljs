(ns linkpage.frontend.main
  (:require [reagent.dom.client :as rd]
            [linkpage.frontend.counter]
            [linkpage.auth.login.frontend]
            [linkpage.rpc.frontend]
            [linkpage.frontend.store :as store]
            [linkpage.frontend.routing :as routing]))

(defn init []
  {:store/state {}
   :store/msgs [[:app/initialized]]})

(defn view []
  (store/view routing/view))

(store/register! {:store/init init})

(defn -main []
  (store/init!)
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/create-root dom-root)]
    (rd/render react-root [view])))
