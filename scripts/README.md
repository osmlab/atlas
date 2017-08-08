# The `atlas` command

This tool runs a number of debugging commands on Atlas files locally.

## Setup

Clone the atlas repository to some folder in your system and create an `ATLAS` environment variable that points to the folder where the code was cloned. Add `$ATLAS/scripts` to your `PATH`.

## Use

Run the `atlas` command, and the standard out will be full of options!

# The `shards` command

## Setup

Same as above

## Use

### Using a regular latitude & longitude

```
shards -sharding=slippy@10 -location=37.786529,-122.397158
```

or

```
shards -sharding=dynamic@/path/to/sharding/tree.txt -location=37.786529,-122.397158
```

### Using a WKT Point

```
shards -sharding=slippy@10 "-wktPoint=POINT (-122.397158 37.786529)"
```