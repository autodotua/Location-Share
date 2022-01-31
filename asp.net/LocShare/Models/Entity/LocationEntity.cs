namespace LocShare.Models.Entity
{
    using Newtonsoft.Json;
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    [Table("location")]
    public partial class LocationEntity
    {
        [Key]
        [Column("id")]
        [JsonProperty("id")]
        public int Id { get; set; }

        [Required]
        [StringLength(50)]
        [Column("username")]
        [JsonProperty("username")]
        public string Username { get; set; }

        [Column("latitude")]
        [JsonProperty("latitude")]
        public double? Latitude { get; set; }

        [Column("longitude")]
        [JsonProperty("longitude")]
        public double? Longitude { get; set; }

        [Column("altitude")]
        [JsonProperty("altitude")]
        public double? Altitude { get; set; }

        [Column("accuracy")]
        [JsonProperty("accuracy")]
        public double? Accuracy { get; set; }

        [Column("speed")]
        [JsonProperty("speed")]
        public double? Speed { get; set; }

        [Column("time")]
        [JsonProperty("time")]
        public DateTime Time { get; set; }
    }
}