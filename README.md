# first_task

Build image:
docker build -t bloomfilter-app .

Run main program:
docker run --rm -it bloomfilter-app ./mainApp

Run tests:
docker run --rm bloomfilter-app ./runTests
