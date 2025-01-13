(ns linkpage.frontend.main
  (:require [reagent.dom.client :as rd]
            [linkpage.frontend.counter]
            [linkpage.auth.login.frontend]
            [linkpage.auth.current-user.frontend :as current-user]
            [linkpage.rpc.frontend]
            [linkpage.frontend.store :as store]
            [linkpage.frontend.routing :as routing]))

(defn view []
  (store/view
   #(current-user/view % routing/view)))

(defn -main []
  (store/initialize!)
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/create-root dom-root)]
    (rd/render react-root [view])))
