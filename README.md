# first_task

Build image:
docker build -t bloomfilter-app .

Run main program:
docker volume create bloomfilter-data
docker run --rm -it -v bloomfilter-data:/usr/src/app/build/data bloomfilter-app ./mainApp


Run tests:
docker run --rm bloomfilter-app ./runTests