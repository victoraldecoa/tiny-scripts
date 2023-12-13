#!/usr/bin/env bb

; example:
; ./weekdays-in-month.clj --month 12 --append ", worked 8 hours in a script for creating an invoice"

(require '[clojure.string :as str])
(require '[babashka.cli :as cli])
(import '[java.time DayOfWeek LocalDate YearMonth])
(import '[java.time.format DateTimeFormatter])

(defn day-of-week-int
  [^LocalDate date]
  (.getValue (.getDayOfWeek date)))

(defn weekday?
  [^LocalDate date]
  (<= (day-of-week-int date) (.getValue DayOfWeek/FRIDAY)))

(defn formatted
  [^LocalDate date]
  {:formatted (.format date (DateTimeFormatter/ofPattern "EEEE, MMMM d, yyyy"))
   :date date})

(defn log-form
  [log {:keys [formatted date]}]
  (str formatted ": 7 hours - " log (when (= (.getDayOfWeek date) DayOfWeek/FRIDAY) "\n")))

(def cli-options {:month {:coerce :long}
                  :year {:default (.getYear (LocalDate/now)) :coerce :long}
                  :language {:default :en :coerce :keyword}
                  :append {:coerce :string}
                  :file {:coerce :string}})

(defn logged-work
  [{:keys [from to log month year skip]}]
  (->> (range from to)
       (remove skip)
       (map #(LocalDate/of year month %))
       (filter weekday?)
       (map formatted)
       (map (partial log-form log))
       (str/join "\n")))

(defn run
  ([{:keys [year month] :as args}
    {:keys [work-log] :as work}]
   (let [last-day (.lengthOfMonth (YearMonth/of year month))
         ranges (partition 2 1 (conj (vec (keys work-log)) (inc last-day)))]
     (->> ranges
          (map #(logged-work (merge args work {:from (first %)
                                               :to (last %)
                                               :log (work-log (first %))})))
          (str/join "\n"))))
  ([]
   (let [{:keys [file] :as args} (cli/parse-opts *command-line-args* {:spec cli-options})
         work (read-string (slurp file))]
     (println (run args work)))))

(run)
