#!/usr/bin/env bb

; example:
; ./weekdays-in-month.clj --month 12 --append ", worked 8 hours in a script for creating an invoice"

(require '[clojure.string :as str])
(require '[babashka.cli :as cli])
(import '[java.time DayOfWeek LocalDate YearMonth])
(import '[java.time.format DateTimeFormatter])

(defn weekday?
  [^LocalDate date]
  (<= (.getValue (.getDayOfWeek date)) (.getValue DayOfWeek/FRIDAY)))

(defn formatted
  [^LocalDate date]
  (.format date (DateTimeFormatter/ofPattern "EEEE, MMMM d, yyyy")))

(defn str-log
  [{:keys [month year]}]
  (->> (range 1 (inc (.lengthOfMonth (YearMonth/of year month))))
       (map #(LocalDate/of year month %))
       (filter weekday?)
       (map formatted)))

(def cli-options {:month {:coerce :long}
                  :year {:default (.getYear (LocalDate/now)) :coerce :long}
                  :append {:coerce :string}})

(let [{:keys [append] :as args} (cli/parse-opts *command-line-args* {:spec cli-options})]
  (println
   (->> args
        str-log
        (map #(str % append))
        (str/join "\n"))))

