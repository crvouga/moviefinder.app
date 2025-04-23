(ns app.frontend.screen
  (:require [app.frontend.route :as route]
            [core.program :as p]
            [clojure.core.async :as a]))

(defn- fallback [] [:screen/profile])

(defn saga [p]
  (p/put! p [::set-screen (p/eff! p [::get-screen])])

  (a/go-loop []
    (let [msg (a/<! (p/take! p :screen/clicked-link))]
      (p/put! p [::set-screen (second msg)])
      (p/eff! p [::push-screen! (second msg)])
      (recur)))

  (a/go-loop []
    (let [msg (a/<! (p/take! p ::got-screen))]
      (p/put! p [::set-screen (second msg)])
      (recur)))

  (doseq [event ["popstate" "pushstate" "replacestate"]]
    (js/window.addEventListener event #(p/put! p [::got-screen (p/eff! p [::get-screen])]))))

(defmethod p/reducer ::set-screen [state msg]
  (assoc state ::screen (second msg)))

(defmethod p/eff! ::push-screen! [_ msg]
  (let [encoded-route (route/encode (second msg))]
    (js/window.history.pushState nil nil (str "/" encoded-route))))

(defmethod p/eff! ::get-screen [_]
  (or (route/get!) (fallback)))

;; 
;; 
;; 
;; 
;; 

(defn screen-name [i] (-> i ::screen first))

(defn screen-payload [i] (-> i ::screen second))

(def ^:private view-screen-by-name! (atom {}))

(defn register! [name view-screen]
  (println "register!" name view-screen)
  (swap! view-screen-by-name! assoc name view-screen))

(defn view [input]
  (let [current-screen (-> input :read! deref ::screen (or (fallback)))
        current-screen-name (first current-screen)]
    [:div.w-full.h-full.bg-black
     [:code (pr-str {:current-screen current-screen})]
     (for [[screen-name view-screen] @view-screen-by-name!]
       ^{:key screen-name}
       [:div.w-full.h-full.overflow-hidden.flex.flex-col
        {:data-screen-name screen-name
         :class (when (not= screen-name current-screen-name) "hidden")}
        (if view-screen
          [view-screen input]
          [:div "No screen found for " (pr-str screen-name)])])]))