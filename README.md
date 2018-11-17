# WeiboData

The public data for our experiment about user preference and information polarization.

## Data description

- Verification data (7 features)
   1. Bot index in the group;
   2. Poster id;
   3. Source id;
   4. Time;
   5. Content;
   6. Classified by FastText algorithm;
   7. Verification (0 for rejected; 1 for approved)
- Exposed weibos (classified by volunteers)
   1. BotId : weibo id;
   2. id : combined weibo id;
   3. groupName : bot index;
   4. time
   5. isE : 1 for entertainment content
   6. isT : 1 for sci-tech content
   7. bzz : volunteer name
   8. userId : poster id
   9. content : text content
- Personal social networks (in Pajek format)
- Bot source code & profiles


## Experiment

In order to explore the extent and pathway of preferences affecting news consumption, we propose an experimental approach by using intelligent social bots. Social bots are generally considered to be harmful, although some of them are benign and, in principle, innocuous or even helpful. Therefore, social bots are often the subject of research that needs to be eliminated, but researchers have yet to recognize their potential value as a powerful tool in social network analysis. Our social bots can use text classification algorithms to simulate the selection behavior of users on content and information sources, thereby simulating the evolution of personal social networks of users with specific preferences. By analyzing the personal social networks of these robots and the information obtained from them, we can clearly show the route of user preferences affecting information polarization, thus revealing the complex interactions behind information polarization.

### Social bot design

A social bot is a computer algorithm that automatically interacts with humans on social networks, trying to emulate and possibly alter their behavior. The social bot in our experiment is designed to imitate similarity-based relationship formation, which reflects the human behavior of selecting information and relationships depending on one's preferences (i.e., self-selection).

![](https://github.com/minyongx/WeiboData/blob/master/fig-bot.png) 

Based on the two hypotheses, the workflow of the bots includes five steps. (1) Initially, each bot is assigned 2 or 3 default followings, who mostly post or repost messages consistent with the preference of the bot. (2) A bot will periodically awaken from idleness at a uniformly random time interval. When the bot awakened, it can view the latest messages posted or reposted by its followings. As well known, not all unreaded information can be exposed to users of social networks. A bot can assess only the latest 50 messages (i.e., the maximum amount in the first page on Weibo) after waking up. Please note that we exclude the influence of algorithmic ranking and recommendation systems (i.e., pre-selection) on the information exposure by re-ranking all possible messages according to the descending order of posting time. (3) After viewing the exposed messages, the bot selects only the messages consistent with its preferences. The step depends on the FastText text classification algorithm, and all classification results from the algorithm are further verified by the experimenters to ensure correctness. (4) If there are reposted messages in selected messages, according to directed triadic closure, the bot randomly selects a reposted message and follows the direct source of the reposted message. Please note that Weibo limits direct access to the information about the followings and followers of a user; thus, bots must find new followings through reposting behavior. (5) If the following number reaches the upper limit, the bot stops running; otherwise, the bot becomes idle and waits to wake up again. To avoid legal and moral hazards, the bots in the experiment do not produce or repost any information.

Technically, the bot is built using the Selenium WebDriver API (http://www.seleniumhq.org/projects/webdriver) to drive Google Chrome Browser as a human to navigate the Weibo website. To train the FastText classifier for identifying preferred contents, we use the THUCNews dataset (http://thuctc.thunlp.org), which contains approximately 740 thousand Chinese news texts from 2005 to 2011 and has been marked for 14 classes. We further combine and filter the original classes in the dataset to meet our requirement for recognizing Weibo text. All codes and data for building bots are freely accessible in GitHub (anonymous for review).

### Experimental treatments

We designed two experimental treatments and evaluated the difference in the above response variables. The two treatments were designed to reflect the variance in content preference on the Weibo. The two treatments are an entertainment group (EG, 娱乐 in Chinese) and a science and technology (sci-tech) group (STG, 科技 in Chinese). Bots in EG prefer entertainment messages, including celebrity gossip, fashion, movies, TV shows, music, and such, and bots in STG prefer sci-tech news, including nature, science, engineering, technological advance, digital products, Internet, and so on. Each treatment contained 34 bots who operated on the Weibo platform between 13 March and 28 September 2018. The initial followings come from a large enough user pool, which contained at least 100 candidates for each preference. The idle period of all bots was 2~4 hours. The maximum number of followings for each bot was 120, according to the Dunbar number.

### Standard for text classification

1. Acceptable entertainment content
   + celebrity gossip, fashion, movies, TV shows, and pop music;
   + Explicitly containing the name, account or abbreviation of entertainers.
2. Acceptable sci-tech content
   + nature, science, engineering, technological advances, digital products, and Internet;
   + technical company and university.
3. Common rejected content
   + commercial advertisement;
   + less than 5 Chinese characters.
4. Rejected entertainment content
   + ACG content (i.e., animation, comic, and digital game);
   + art, literature, and personal feeling;
   + simple lyrics and lines;
   + personal leisure activities.
5. Rejected sci-tech content
   + financial or business report of technical or Internet company;
   + price of digital products;
   + military equipment;
   + daily skills;
   + constellations and divination;
   + weather forecast;
   + environmental conservation;
   + documentary with irrelevant content.
