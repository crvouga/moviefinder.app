(ns app.frontend.screen
  (:require
   [app.frontend.mod :as mod]
   [app.frontend.route :as route]
   [clojure.core.async :as a]
   [lib.browser :as browser]
   [lib.program :as p]))

;; 
;; 
;; 

(defn- fallback [] [:screen/home])
(defn to-screen [i] (-> i ::screen))
(defn to-screen-name [i] (-> i to-screen first))
(defn to-screen-payload [i] (-> i to-screen second))

(defn- logic [i]
  (p/reg-reducer
   i ::set-screen
   (fn [state [_ screen]] (assoc state ::screen screen)))

  (p/reg-eff
   i ::push-screen!
   (fn [[_ screen]]
     (-> screen route/encode browser/push-state!)))

  (p/reg-eff
   i ::get-screen
   (fn [_] (or (route/get!) (fallback))))

  (a/go
    (p/put! i [::set-screen (p/eff! i [::get-screen])])
    (p/put! i [:screen/screen-changed (p/eff! i [::get-screen])]))

  (p/take-every!
   i :screen/clicked-link
   (fn [[_ screen]]
     (p/put! i [::set-screen screen])
     (p/eff! i [::push-screen! screen])
     (p/put! i [:screen/screen-changed screen])))

  (p/take-every!
   i :screen/push
   (fn [[_ screen]]
     (p/put! i [::set-screen screen])
     (p/eff! i [::push-screen! screen])
     (p/put! i [:screen/screen-changed screen])))

  (p/take-every!
   i ::got-screen
   (fn [[_ screen]]
     (p/put! i [::set-screen screen])
     (p/put! i [:screen/screen-changed screen])))

  (a/go-loop []
    (let [_ (a/<! (browser/history-event-chan))]
      (println "history event")
      (p/put! i [::got-screen (p/eff! i [::get-screen])])
      (recur))))


;; 
;; 
;; 
;; 
;; 



(defn- concatv [node children]
  (vec (concat node children)))


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
