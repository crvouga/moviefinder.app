(ns app.frontend.screen
  (:require [app.frontend.route :as route]
            [core.program :as p]
            [clojure.core.async :as a]
            [app.frontend.mod :as mod]))

;; 
;; 
;; 

(defn- fallback [] [:screen/home])

(defn- logic [i]

  (p/reg-reducer
   i
   ::set-screen
   (fn [state msg] (assoc state ::screen (second msg))))

  (p/reg-eff
   i
   ::push-screen!
   (fn [eff]
     (println "push-screen!" eff)
     (let [encoded-route (route/encode (second eff))]
       (js/window.history.pushState nil nil (str "/" encoded-route)))))

  (p/reg-eff
   i
   ::get-screen
   (fn [_]
     (or (route/get!) (fallback))))

  (a/go-loop []
    (p/put! i [::set-screen (p/eff! i [::get-screen])])
    (let [msg (a/<! (p/take! i :screen/clicked-link))]
      (p/put! i [::set-screen (second msg)])
      (p/eff! i [::push-screen! (second msg)])
      (recur)))

  (a/go-loop []
    (let [msg (a/<! (p/take! i :screen/push))]
      (p/put! i [::set-screen (second msg)])
      (p/eff! i [::push-screen! (second msg)])
      (recur)))

  (a/go-loop []
    (let [msg (a/<! (p/take! i ::got-screen))]
      (p/put! i [::set-screen (second msg)])
      (p/put! i [:screen/screen-changed (second msg)])
      (recur)))

  (doseq [event ["popstate" "pushstate" "replacestate"]]
    (js/window.addEventListener event #(p/put! i [::got-screen (p/eff! i [::get-screen])]))))


;; 
;; 
;; 
;; 
;; 

(defn screen-name [i] (-> i ::screen first))

(defn screen-payload [i] (-> i ::screen second))

(defn- concatv [node children]
  (vec (concat node children)))



(defn- view-screen-hidden-css [i screen-name & children]
  (let [current-screen-name (-> i ::screen (or (fallback)) first)]
    [:<>
     (concatv
      [:div.w-full.h-full.overflow-hidden.flex.flex-col
       {:data-screen-name screen-name
        :class (when (not= screen-name current-screen-name) "hidden")}]
      children)]))

(defn- view-screen-hidden-conditional [i screen-name & children]
  (let [current-screen-name (-> i ::screen (or (fallback)) first)]
    (if (= screen-name current-screen-name)
      (concatv
       [:div.w-full.h-full.overflow-hidden.flex.flex-col
        {:data-screen-name screen-name}]
       children)
      nil)))

(defn view-screen [i screen-name & children]
  (let [current-screen-name (-> i ::screen (or (fallback)) first)]
    [:<>
     (concatv
      [:div.w-full.h-full.overflow-hidden.flex.flex-col
       {:data-screen-name screen-name
        :class (when (not= screen-name current-screen-name) "hidden")}]
      children)]))

;; 
;; 
;; 

(mod/reg {:mod/name :mod/screen
          :mod/logic-fn logic})
