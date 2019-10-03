(ns chat.events
  (:require
   [chat.firebase :as firebase]
   [re-frame.core :as re-frame]
   [chat.db :as db]
   [cljs.reader :as reader]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [goog.object :as gobj]
   [secretary.core :as secretary]
   ))

(re-frame/reg-event-fx
 ::initialize-db
 (fn-traced [_ _]
            {:db db/default-db
             :local-storage {:op :get
                             :key "chat/user"
                             :dispatch ::user-read-ok
                             }}))

(re-frame/reg-event-fx
 ::user-read-ok
 (fn-traced [db [_ user]]
            (js/console.log user)
            {:db (assoc db :user user)
             :navigate (if user "#/chat/clj-group" "/")
             }))

(re-frame/reg-event-db
 ::set-route
 (fn-traced [db [_ {:keys [route params]}]]
            (assoc db :route {:route route :params params})))

(re-frame/reg-event-fx
 ::auth-ok
 (fn-traced [db [_ resp]]
            (let [
                  profile (gobj/getValueByKeys resp "additionalUserInfo" "profile")
                  user {:username (gobj/get profile "login")
                        :fullname (gobj/get profile "name")
                        :bio (gobj/get profile "bio")
                        :avatar (gobj/get profile "avatar_url")}]
              {:db (assoc db :user user)
               :navigate "#/chat/clj-group"
               :local-storage {:data user
                               :key "chat/user"
                               :op :set}})))

 (re-frame/reg-fx
  :local-storage
  (fn [{:keys [data key op dispatch]}]
    ;; (js/console.log ">>>>>")
    ;; (js/console.log (.setItem js/localStorage key (pr-str data)))
    ;; (js/console.log data key op dispatch)
    (case op
      :set (.setItem js/localStorage key (pr-str data))
      ;; :set (js/console.log "SETTED")
      :get  (->> (.getItem js/localStorage key)
                 (reader/read-string)
                 (vector dispatch)
                 (re-frame/dispatch))
      ;; (js/console.log op)
      )
    ))

(re-frame/reg-fx
 :navigate
 (fn [route _]
   ;; (js/console.log route)
   ;; (js/console.log (.. js/location -hash))
   (secretary/dispatch! route)
   (set! (.. js/location -hash) route)
 ))

(re-frame/reg-event-db
 ::auth-fail
 (fn-traced [db [_ err]]
            db))



(re-frame/reg-event-fx
 ::sign-in
 (fn-traced [_ _]
            {:firebase {:op :auth
                        :on-success ::auth-ok
                        :on-fail ::auth-fail}}))

(defmulti firebase (fn [m] (:op m)))

(defmethod firebase :auth [{:keys [on-success on-fail]}]
  (-> (firebase/sign-in-with-github)
      (.then #(re-frame/dispatch [on-success %]))
      (.catch #(re-frame/dispatch [on-fail %]))))

(re-frame/reg-fx
 :firebase
 (fn [effect]
   (firebase effect)))
