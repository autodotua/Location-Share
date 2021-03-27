namespace LocShare.Test
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Data.Entity.Spatial;

    public partial class login_user
    {
        [Key]
        [StringLength(50)]
        public string name { get; set; }

        [StringLength(255)]
        public string display_name { get; set; }

        [Required]
        [StringLength(32)]
        public string password { get; set; }

        [Required]
        [StringLength(255)]
        public string group_name { get; set; }

        public DateTime? last_update_time { get; set; }
    }
}
