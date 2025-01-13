(ns linkpage.auth.login.frontend
  (:require
   [clojure.core.async :refer [go <! timeout]]
   [linkpage.frontend.routing :as routing]
   [linkpage.frontend.store :as store]
   [linkpage.frontend.ui.button :as button]
   #_[linkpage.frontend.ui.form :as form]
   [linkpage.frontend.ui.text-field :as text-field]))

(defn init []
  {:store/state {::current-user [:result/not-asked]}})

(defmulti step store/msg-type)

(defmethod step :default [i] i)

(defmethod step ::clicked-get-current-user [i]
  (-> i
      (update-in [:store/state] assoc ::current-user [:result/loading])
      (update-in [:store/effects] conj [::get-current-user!])))

(defn get-current-user! []
  (go
    (<! (timeout 3000))
    [:result/ok
     {:user/user-id 1
      :user/username "test-user"
      :user/email "my-email"}]))


(defmethod store/effect! ::get-current-user! [i]
  (go
    (let [user (<! (get-current-user!))]
      (store/dispatch! i [::got-current-user user]))))

(defmethod step ::got-current-user [i]
  (-> i
      (assoc-in [:store/state ::current-user] (store/msg-payload i))))

(defmulti view-current-user-status (fn [i] (-> i :store/state ::current-user first)))

(defmethod view-current-user-status :result/not-asked [_]
  [:div "Not asked yet"])

(defmethod view-current-user-status :result/loading [_]
  [:div "Loading..."])

(defmethod view-current-user-status :result/ok [i]
  [:div (str "Current user: " (-> i :store/state ::current-user second :user/username))])

(defmethod view-current-user-status :result/err [_]
  [:div "Error!"])

(defn view-get-current-user-button [i]
  [:button {:on-click #(store/dispatch! i [::clicked-get-current-user])
            :disabled (-> i :store/state ::current-user first (= :result/loading))} "Get current user"])

;; 
;; 
;; 

(defmethod step ::inputted-phone-number [i]
  (-> i
      (assoc-in [:store/state ::phone-number] (store/msg-payload i))))

(defmethod step ::submitted-send-code-form [i]
  (-> i
      (update-in [:store/state] assoc ::send-code [:result/loading])
      (update-in [:store/effects] conj [:rpc/send! {:rpc/msg [:login/send-code {:user/phone-number (-> i :store/state ::phone-number)}]
                                                    :rpc/dispatch! #(vector ::sent-code %)}])))

(defn sending-code? [i]
  (-> i :store/state ::send-code first (= :result/loading)))

(defn view-send-code-form [i]
  [:form
   {:on-submit
    #(do
       (.preventDefault %)
       (store/dispatch! i [::submitted-send-code-form]))}
   [text-field/view
    {:text-field/label "Phone Number"
     :text-field/value (-> i :store/state ::phone-number)
     :text-field/required? true
     :text-field/disabled? (sending-code? i)
     :text-field/on-change #(store/dispatch! i [::inputted-phone-number %])}]
   [button/view
    {:button/type :button-type/submit
     :button/loading? (sending-code? i)
     :button/label "Send code"}]])

;; 
;; 
;; 

(defn view [i]
  [:main
   [:section
    [:button {:on-click #(store/dispatch! i [:routing/clicked-link [:route/counter]])} "my 123"]
    (view-send-code-form i)
    (view-get-current-user-button i)
    (view-current-user-status i)]])

(defmethod routing/view :route/login [i]
  (view i))

(store/register! {:store/init init :store/step step})

