# Quiz Leaderboard System 

This repository contains the backend integration assignment for the Bajaj Finserv Health JAVA Qualifier.

## Problem Solved
The system connects to an external validator API, simulating a quiz show where data may be delivered multiple times. The application achieves the following:
1. Polls the `/quiz/messages` API 10 times with a strict 5-second delay.
2. Deduplicates incoming data using a combination of `roundId` + `participant`.
3. Aggregates the unique scores per participant.
4. Generates a sorted leaderboard.
5. Submits the final calculated leaderboard via a POST request to `/quiz/submit`.

## Tech Stack
* **Language:** Java
* **Dependencies:** None. Built exclusively using `java.net.http.HttpClient` and standard Java Collections for efficient data deduplication and aggregation.

## How to Run
1. Ensure you have Java installed.
2. Compile the file: `javac QuizLeaderboard.java`
3. Run the application: `java QuizLeaderboard`
