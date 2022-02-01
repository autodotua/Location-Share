using Newtonsoft.Json;
using System;

namespace LocShare.Dto
{
    public class LocationDto
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("username")]
        public string Username { get; set; }

        [JsonProperty("latitude")]
        public double? Latitude { get; set; }

        [JsonProperty("longitude")]
        public double? Longitude { get; set; }

        [JsonProperty("altitude")]
        public double? Altitude { get; set; }

        [JsonProperty("accuracy")]
        public double? Accuracy { get; set; }

        [JsonProperty("speed")]
        public double? Speed { get; set; }

        [JsonProperty("time")]
        public DateTime Time { get; set; }
    }
}