(ns linkpage.auth.login.frontend
  (:require
   [linkpage.frontend.routing :as routing]
   [linkpage.frontend.store :as store]
   [linkpage.core.result :as result]
   [linkpage.frontend.ui.button :as button]
   [linkpage.frontend.ui.text-field :as text-field]))

(defmulti step store/msg-type)

(defmethod step :default [i] i)

(defmethod step :store/initialized [i]
  (-> i
      (assoc-in [:store/state ::send-code] [:result/not-asked])
      (assoc-in [:store/state ::verify-code] [:result/not-asked])))


(defmethod step ::submitted-send-code-form [i]
  (-> i
      (update-in [:store/state] assoc ::send-code [:result/loading])
      (update-in [:store/effs] conj [:rpc/send! {:rpc/req [:login/send-code {:user/phone-number (-> i :store/state ::phone-number)}]
                                                 :rpc/res #(vector ::sent-code %)}])))

(defmethod step ::sent-code [i]
  (let [sent-code (store/msg-payload i)]
    (println "sent-code" sent-code)
    (-> i
        (update-in [:store/state] assoc ::send-code sent-code)
        (update-in [:store/msgs] conj (when (result/ok? sent-code)
                                        (println "pushing" "sent-code=" sent-code)
                                        [:routing/push [:route/login-verify-code sent-code]])))))

(defmethod step ::inputted-phone-number [i] (-> i (assoc-in [:store/state ::phone-number] (store/msg-payload i))))

(defn sending-code? [i] (-> i :store/state ::send-code first (= :result/loading)))

(defmethod routing/view :route/login [i]
  [:main.container
   [:header [:h1 "Login with SMS"]]
   [:section
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
       :button/label "Send code"}]]]])

(defmethod step ::inputted-code [i] (-> i (assoc-in [:store/state ::code] (store/msg-payload i))))
(defmethod step ::submitted-verify-code-form [i]
  (-> i
      (update-in [:store/state] assoc ::verify-code [:result/loading])
      (update-in [:store/effs] conj [:rpc/send! {:rpc/req [:login/verify-code {:user/code (-> i :store/state ::code)}]
                                                 :rpc/res #(vector ::verified-code %)}])))
(defmethod step ::verified-code [i]
  (-> i
      (update-in [:store/state] assoc ::verify-code (store/msg-payload i))))

(defmethod routing/view :route/login-verify-code [i]
  [:main.container
   [:header [:h1 "Verify Code"]]
   [:section
    [:form
     {:on-submit
      #(do
         (.preventDefault %)
         (store/dispatch! i [::submitted-verify-code-form]))}
     [text-field/view
      {:text-field/label "Code"
       :text-field/value (-> i :store/state ::code)
       :text-field/required? true
       :text-field/disabled? (sending-code? i)
       :text-field/on-change #(store/dispatch! i [::inputted-code %])}]
     [button/view
      {:button/type :button-type/submit
       :button/loading? (sending-code? i)
       :button/label "Send code"}]]]])

(store/register-step! step)

