# Agent Design

## Input

* input:
  * ^creatures: Creatures
    * ^creature: Creature
      * ^id: Int
      * ^self: Boolean?
        
## Output

* output:
  * ^action: Action
    * ^type: String \[sleep,move,go-down,go-up\]

## Goal Hierarchy

* BeatTheChamp
  * DecideOnAction
    * Survive -- seek to avoid an immediate death - run away, apply healing
    * Battle -- use melee or ranging attacks to assault enemies, use skills/items to help if necessary
    * Plunder -- pickup known useful items
    * Explore -- navigate to unknown tiles
    * Experiment -- try unknown items to evaluate results, apply known useful permanent items if relevant
    * Sleep -- fallback if enable to perform a useful action
