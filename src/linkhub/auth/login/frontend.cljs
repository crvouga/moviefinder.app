(ns linkhub.auth.login.frontend
  (:require [linkhub.frontend.store :as store]
            [clojure.core.async :as async]
            [linkhub.frontend.routing :as routing]
            [linkhub.frontend.ui.text-field :as text-field]
            [linkhub.frontend.ui.button :as button]))

(defn init []
  {:store/state {::current-user [:result/not-asked]}})

(defmulti step store/event-type)

(defmethod step :default [i] i)

(defmethod step ::clicked-get-current-user [i] 
  (-> i
      (update-in [:store/state] assoc ::current-user [:result/loading])
      (update-in [:store/effects] conj [::get-current-user!])))

(defmethod step ::got-current-user [i] 
  (-> i
      (assoc-in [:store/state ::current-user] (-> i :store/event second))))


(defn get-current-user! []
  (async/go
    (async/<! (async/timeout 3000)) 
    [:result/ok
     {:user/user-id 1 
      :user/username "test-user" 
      :user/email "my-email"}]))

(defmethod store/effect! ::get-current-user! [i]
  (async/go
    (let [user (async/<! (get-current-user!))]
      ((:store/dispatch! i) [::got-current-user user]))))

(defmulti view-status (fn [i] (-> i :store/state ::current-user first)))

(defmethod view-status :result/not-asked [_]
  [:div "Not asked yet"])

(defmethod view-status :result/loading [_]
  [:div "Loading..."])

(defmethod view-status :result/ok [i]
  [:div (str "Current user: " (-> i :store/state ::current-user second :user/username))])

(defmethod view-status :result/err [_]
  [:div "Error!"])

(defn view-get-current-user-button [i]
  [:button {:on-click #((:store/dispatch! i) [::clicked-get-current-user])
            :disabled (-> i :store/state ::current-user first (= :result/loading))} "Get current user"])

;; 
;; 
;; 

(defmethod step ::inputted-phone-number [i]
  (-> i 
      (assoc-in [:store/state ::phone-number] (store/event-payload i))))

(defmethod step ::submitted-send-code-form [i]
  (-> i
      (update-in [:store/state] assoc ::send-code [:result/loading])
      (update-in [:store/effects] conj [::send-code!])))

(defmethod store/effect! ::send-code! [i]
  (println "Sending code..." i))

(defn loading? [i]
  (-> i :store/state ::send-code first (= :result/loading)))

(defn view-send-code-form [i]
  [:form {:on-submit #(do (.preventDefault %) 
                          ((:store/dispatch! i) [::submitted-send-code-form]))}
    [text-field/view
     {:text-field/label "Phone Number"
      :text-field/value (-> i :store/state ::phone-number)
      :text-field/required? true
      :text-field/disabled? (loading? i)
      :text-field/on-change #((i :store/dispatch!) [::inputted-phone-number %])}]
    [button/view
     {:button/type :button-type/submit
      :button/loading? (loading? i)
      :button/label "Send code"}]])

;; 
;; 
;; 

(defn view [i]
  [:main
   [:section
    [:button {:on-click #((:store/dispatch! i) [:routing/clicked-link [:route/counter]])} "my 123"]
    (view-send-code-form i)
    (view-get-current-user-button i)
    (view-status i)]])

(defmethod routing/view :route/login [i]
  (view i))

(store/register! {:store/init init :store/step step})

