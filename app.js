const express = require("express");
const app = express();
const mongoose = require("mongoose");
const Listing = require("./models/listing.js");
const path = require("path");
const methodOverride = require("method-override");
const ejsMate = require("ejs-mate");

app.set("view engine", "ejs");
app.set("views", path.join(__dirname, "views"));
app.use(express.urlencoded({ extended: true }));
app.use(methodOverride("_method"));
app.engine("ejs", ejsMate);
app.use(express.static(path.join(__dirname, "/public")));

const MONGO_URL = process.env.MONGO_URL || "mongodb://mongodb:27017/hustleBust";
async function main() {
  await mongoose.connect(MONGO_URL);
}

main()
  .then(() => {
    console.log("Connected to database");
  })
  .catch((err) => {
    console.log(err);
  });

app.get("/", async (req, res) => {
  const allListings = await Listing.find({});
  res.render("listings/index.ejs", { allListings });
});

//Index route
app.get("/listings", async (req, res) => {
  const allListings = await Listing.find({});
  res.render("listings/index.ejs", { allListings });
});

//New listing form route
app.get("/listings/new", (req, res) => {
  res.render("listings/new.ejs");
});

//Show individual listing Route
app.get("/listings/:id", async (req, res) => {
  let { id } = req.params;
  const listing = await Listing.findById(id);
  res.render("listings/show.ejs", { listing });
});

//Create new listing route
app.post("/listing", async (req, res) => {
  let newListing = await new Listing(req.body.listing);
  newListing.save();
  res.redirect("/listings");
});

//edit listing form route
app.get("/listings/:id/edit", async (req, res) => {
  let { id } = req.params;
  const listing = await Listing.findById(id);
  res.render("listings/edit.ejs", { listing });
});

//Update listing route
app.put("/listings/:id", async (req, res) => {
  let { id } = req.params;
  await Listing.findByIdAndUpdate(id, { ...req.body.listing });
  res.redirect(`/listings/${id}`);
});

//Delete Listing Route
app.delete("/listings/:id", async (req, res) => {
  let { id } = req.params;
  await Listing.findByIdAndDelete(id);
  res.redirect("/listings");
});

// app.get('/testListing', async (req, res) => {
//   let newListing = new Listing({
//     title: 'Lida Villa',
//     description: 'Beachy place',
//     price: 20000,
//     location: 'Clifton, Karachi',
//     country: 'Pakistan'
//   })

//   await newListing.save()
//   console.log(newListing)

//   res.send('testing successful')
// })

app.listen(4000, "0.0.0.0", () => {
  console.log("Server is listening on port 3000");
});

