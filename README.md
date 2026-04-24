## My Approach & Thought Process
When tackling this assignment, I focused on keeping the solution lightweight and avoiding unnecessary external dependencies:
* **Deduplication Strategy:** Instead of complex loops, I used a `HashSet` containing a composite string of `roundId_participant`. This allows for highly efficient $O(1)$ lookups to instantly drop duplicate API events.
* **JSON Parsing:** Since the payload is relatively flat and predictable, I opted to use standard Java Regular Expressions (`java.util.regex.Matcher`) to extract the data. This removed the need to force the evaluator to download heavy libraries like Jackson or Gson just to run a single file.
* **API Constraints:** I used `Thread.sleep(5000)` to strictly adhere to the validator's rate-limiting requirement.

## Challenges & Learnings
* Ensuring the score aggregation was tied strictly to the deduplication step so that re-transmitted API payloads wouldn't inflate a participant's score.
