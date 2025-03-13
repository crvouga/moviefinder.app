(ns app.frontend.screen
  (:require
   [app.frontend.store :as store]
   [app.frontend.route :as route]))

(def fallback [:screen/home])

(defn- get-route! []
  (or (route/get!) fallback))

(store/register!
 :store/initialized
 (fn [i]
   (-> i
       (update-in [:store/state] assoc ::screen nil)
       (update-in [:store/effs] concat [[::get-route!]
                                        [::subscribe-route!]])))

 ::got-screen
 (fn [i]
   (-> i
       (assoc-in [:store/state ::screen] (store/to-msg-payload i))))

 ::screen-changed
 (fn [i]
   (-> i
       (assoc-in [:store/state ::screen] (store/to-msg-payload i))))

 :screen/clicked-link
 (fn [i]
   (let [screen-new (store/to-msg-payload i)
         current-screen (-> i :store/state ::screen)]
     (if (= screen-new current-screen)
       i
       (-> i
           (assoc-in [:store/state ::screen] screen-new)
           (update-in [:store/effs] conj [::push! screen-new])))))

 :screen/push
 (fn [i]
   (let [screen-new (store/to-msg-payload i)]
     (-> i
         (assoc-in [:store/state ::screen] screen-new)
         (update-in [:store/effs] conj [::push! screen-new])))))


(store/register-eff!
 ::get-route!
 (fn [i]
   (store/put! i [::got-screen (get-route!)]))

 ::push!
 (fn [i]
   (let [route (store/to-eff-payload i)
         encoded (route/encode route)]
     (js/window.history.pushState nil nil encoded)))

 ::subscribe-route!
 (fn [i]
   (doseq [event ["popstate" "pushstate" "replacestate"]]
     (js/window.addEventListener event #(store/put! i [::screen-changed (get-route!)])))))


(defn screen-name [i]
  (-> i :store/state ::screen first))

(defn screen-payload [i]
  (-> i :store/state ::screen second))

(def view-screen-by-name! (atom {}))

(defn register! [name view-screen]
  (println "reg! name" name)
  (swap! view-screen-by-name! assoc name view-screen))
(defn view [i]
  (let [current-screen (-> i :store/state ::screen (or fallback))
        current-screen-name (first current-screen)]
    [:div.w-full.h-full.bg-black
     (for [[screen-name view-screen] @view-screen-by-name!]
       ^{:key screen-name}
       [:div.w-full.h-full.overflow-hidden.flex.flex-col
        {:data-screen-name screen-name
         :class (when (not= screen-name current-screen-name) "hidden")}
        (if view-screen
          [view-screen i]
          [:div "No screen found for " (pr-str screen-name)])])]))
