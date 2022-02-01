namespace LocShare.Models
{
    using Newtonsoft.Json;
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    [Table("login_user")]
    public partial class UserEntity
    {
        [Key]
        [StringLength(50)]
        [Column("name")]
        public string Name { get; set; }

        [StringLength(255)]
        [Column("display_name")]
        public string DisplayName { get; set; }

        [Required]
        [StringLength(32)]
        [Column("password")]
        public string Password { get; set; }

        [StringLength(255)]
        [Column("group_name")]
        public string GroupName { get; set; } = "";

        [Column("last_update_time")]
        public DateTime? LastUpdateTime { get; set; }

        [Column("last_location_id")]
        public int? LastLocationId { get; set; }

        public override string ToString()
        {
            return Name + "£¨" + DisplayName + "£© - " + GroupName;
        }
    }
}