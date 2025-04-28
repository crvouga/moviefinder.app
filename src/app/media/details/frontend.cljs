(ns app.media.details.frontend
  (:require
   [app.frontend.db :as db]
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [clojure.core.async :refer [<! go-loop]]
   [lib.program :as p]
   [lib.ui.image :as image]
   [lib.ui.top-bar :as top-bar]))

;; 
;; 
;; 
;; 

(defn- logic [i]
  (p/reg-reducer i ::set-media-details (fn [state msg] (assoc state ::media-details (second msg))))

  (go-loop []
    (let [_ (<! (p/take! i ::clicked-back))]
      (p/put! i [:screen/push [:screen/home]])
      (recur))))

;; 
;; 
;; 
;; 


(defn view-backdrop [media]
  [image/view
   {:class "w-full aspect-video object-cover"
    :image/url (:media/backdrop-url media)
    :image/alt (:media/title media)}])

(defn view-top-bar [i media]
  [top-bar/view
   {:top-bar/on-back #(p/put! i [::clicked-back])
    :top-bar/title (:media/title media)}])

(defn view-title [media]
  [:div.text-3xl.font-bold (:media/title media)])

(defn view-overview [media]
  [:div.text-lg.text-neutral-300 (:media/overview media)])



(defn view [i]
  (let [payload (screen/screen-payload i)
        media (db/to-entity i (:media/id payload))]
    [screen/view-screen i :screen/media-details
     [view-top-bar i media]
     [view-backdrop media]
     [:div.w-full.text-center.flex.flex-col.items-center.justify-center.p-6.gap-3
      [view-title media]
      [view-overview media]]]))

(mod/reg
 {:mod/name :mod/media-details
  :mod/view-fn view
  :mod/logic-fn logic})
