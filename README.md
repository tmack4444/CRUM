# CRUM
Computer Resource Usage Monitor, MSCS 710 spring 2021

Authors: Tom Mackinson, Samantha Berry, Paul Ippolito


## About

CRUM is an application for monitoring resource usage on a user's windows Computer

It makes use of a library called OSHI (https://github.com/oshi/oshi) to gather usage statistics for various parts of the Computer every second. CRUM is designed to be relatively lightweight, allowing a user to run it alongside other programs to check for bottlenecks, performance issues, or any other problems. Limiting CRUM's impact ensures that overall performance is not hampered by checking statistics while using other applications, thus giving incorrect readings of performance.

This information is stored in an SQLite database (located in C:/tmp), and is presented through a GUI using Swing, and JFreeChart to present graphs of certain statistics.

To run CRUM, simply download the CRUM-1.0.jar (you can find it in the target folder). From there simply run the jar. It will automatically create a db, and the tmp folder as necessary.
You will be presented with the main page at first, from there you can navigate to various resource statistics through either the buttons on the main page, or the tabs in the top left corner of the application.

## Resources Gathered

### Main
The main tab provides some simple information about the user's machine. This includes the Model, ID (or serial number), and vendor.

### CPU
The CPU tab presents statistics about the CPU. In the top left corner is the CPU's model number, along the bottom are the number of physical cores, logical cores, overall usage, max clock speed, and the number of processes.

The chart in the center of the GUI displays the current overall usage. This is a combined average of all cores on the CPU. For example, if you are using a computer with 2 cores, and one core is at 100% usage, and the other is at 25% usage, the total usage displayed at that time will be (1 + 0.25)/2 = .625, or 62.5%. Obviously these numbers will vary based on number of cores and the actual usage at a given time.

### RAM

RAM represents the total amount of RAM being used at a given time. in the top left corner, you see the Total RAM that oshi finds installed on your machine, as well as the current amount in use. These are both represented in MB, and the chart in the center of the screen shows the amount of RAM currently in use.

### Network

Network displays the current total Mbps either inbound or outbound across all network devices. Similar to CPU, this number is a total value, incase several different interfaces are in use at a given time. In the top left of the display all MAC addresses are listed, as well as numbers for any traffic usage. The chart displays both inbound and outbound traffic at a given time.

### Disk

Disk tabs are created dynamically based on the number of disks installed on a user's computer. Each disk tab represents a partition on the computer, so there could be multiple tabs that point to one disk. Each tab displays the drive letter, model, total size, total amount in use, and the speed. Speed is represented in KB/s. The speed presented is for the physical disk, so if multiple partitions are on one disk, this could be the same across tabs.
