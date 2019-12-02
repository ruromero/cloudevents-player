import React from "react";
import "typeface-roboto";
import {
  makeStyles,
  AppBar,
  CssBaseline,
  Grid,
  Toolbar,
  Typography
} from "@material-ui/core";
import "./App.css";
import EventSender from "./EventSender";
import Activity from "./Activity";

const useStyles = makeStyles(theme => ({
  root: {
    display: "flex"
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1
  },
  content: {
    flexGrow: 1,
    padding: theme.spacing(3)
  },
  toolbar: theme.mixins.toolbar
}));

const App = () => {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <CssBaseline />
      <AppBar position="fixed" className={classes.appBar}>
        <Toolbar>
          <Typography variant="h6" noWrap>
            CloudEvents player
          </Typography>
        </Toolbar>
      </AppBar>
      <main className={classes.content}>
        <div className={classes.toolbar} />
        <Grid container className={classes.root} spacing={2}>
          <Grid item xs={4}>
            <EventSender />
          </Grid>
          <Grid item xs={8}>
            <Activity />
          </Grid>
        </Grid>
      </main>
    </div>
  );
}

export default App;
