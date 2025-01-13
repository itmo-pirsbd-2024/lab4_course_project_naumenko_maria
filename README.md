# Сервис для объединения векторного и полнотекстового поиска

## Обзор

Сервис позволяет посчитать MRR на датасете MSMARCO, используя:
- векторный поиск(*faiss* + *msmarco-distilbert-base-v3*)
- полнотекстовый поиск (Lucene (BM25))
- объединения двух выборок с помощью RSF
- объединения двух вборок с помощью RRF

## Данные
В качестве данных используется датасет MSMARCO (validation partition). Данный сервис использует данные датасета, сохраненные в csv формате.

## Запуск и тестирование
Сервер запускается в классе **ru.nms.diplom.ircrossfeature.Main**, тестовые данные можно послать в классе **ru.nms.diplom.ircrossfeature.TestApiClient**.

Для запуска векторного поиска необходимо запустить [faiss-api](https://github.com/Marygith/faiss-api).

Для запуска полнотекстового поиска необходимо поднять [lucene-ir-server](https://github.com/Marygith/lucene-ir-server).