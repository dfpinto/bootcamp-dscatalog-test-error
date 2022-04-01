import Navbar from 'components/Navbar';
import Home from 'pages/Home';
import Catalog from 'pages/Catalog';
import { Redirect, Route, Router, Switch  } from 'react-router-dom';
import ProductDetails from 'pages/ProductDetails';
import Admin from 'pages/Admin';
import Auth from 'pages/Admin/Auth';
import history from 'util/history';

const Routes = () => {
  return (
    <Router history={history}>
      <Navbar />
      <Switch>
        <Route path="/" exact>
          <Home />
        </Route>
        <Route path="/products" exact>
          <Catalog />
        </Route>
        <Route path="/products/:productId">
          <ProductDetails />
        </Route>
        <Redirect from="/admin/auth" to="/admin/auth/login" exact/>
        <Route path="/admin/auth">
          <Auth/>
        </Route>
        <Redirect from="/admin" to="/admin/products" exact/>
        <Route path="/admin">
          <Admin />
        </Route>
      </Switch>
    </Router>
  );
}

export default Routes;
