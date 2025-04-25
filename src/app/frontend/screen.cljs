(ns app.frontend.screen
  (:require [app.frontend.route :as route]
            [core.program :as p]
            [clojure.core.async :as a]))

(defn- fallback [] [:screen/home])



(a/go
  (p/put! [::set-screen (p/eff! [::get-screen])])
  #_(a/<! (a/timeout 2000))
  #_(recur))



(a/go-loop []
  (let [msg (a/<! (p/take! :screen/clicked-link))]
    (p/put! [::set-screen (second msg)])
    (p/eff! [::push-screen! (second msg)])
    (recur)))

(a/go-loop []
  (let [msg (a/<! (p/take! :screen/push))]
    (p/put! [::set-screen (second msg)])
    (p/eff! [::push-screen! (second msg)])
    (recur)))

(a/go-loop []
  (let [msg (a/<! (p/take! ::got-screen))]
    (p/put! [::set-screen (second msg)])
    (p/put! [:screen/screen-changed (second msg)])
    (recur)))

(doseq [event ["popstate" "pushstate" "replacestate"]]
  (js/window.addEventListener event #(p/put! [::got-screen (p/eff! [::get-screen])])))

(p/reg-reducer
 ::set-screen
 (fn [state msg] (assoc state ::screen (second msg))))

(p/reg-eff
 ::push-screen!
 (fn [eff]
   (println "push-screen!" eff)
   (let [encoded-route (route/encode (second eff))]
     (js/window.history.pushState nil nil (str "/" encoded-route)))))


(p/reg-eff
 ::get-screen
 (fn [_]
   (or (route/get!) (fallback))))



;; 
;; 
;; 
;; 
;; 

(defn screen-name [i] (-> i ::screen first))

(defn screen-payload [i] (-> i ::screen second))

(def ^:private view-screen-by-name! (atom {}))

(defn register [name view-screen]
  (println "register" name view-screen)
  (swap! view-screen-by-name! assoc name view-screen))

(defn view [input]
  (let [current-screen (-> input ::screen (or (fallback)))
        current-screen-name (first current-screen)]
    [:div.w-full.h-full.bg-black
     #_[:code (-> input ::screen pr-str)]
     (for [[screen-name view-screen] @view-screen-by-name!]
       ^{:key screen-name}
       [:div.w-full.h-full.overflow-hidden.flex.flex-col
        {:data-screen-name screen-name
         :class (when (not= screen-name current-screen-name) "hidden")}
        (if view-screen
          [view-screen input]
          [:div "No screen found for " (pr-str screen-name)])])]))